package jsfca;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import jsfca.preferences.JsfcaPerferenceConstants;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jst.jsf.context.symbol.IBeanInstanceSymbol;
import org.eclipse.jst.jsf.context.symbol.IJavaTypeDescriptor2;
import org.eclipse.jst.jsf.context.symbol.ISymbol;
import org.eclipse.jst.jsf.context.symbol.SymbolFactory;
import org.eclipse.jst.jsf.context.symbol.source.AbstractSymbolSourceProviderFactory;
import org.eclipse.jst.jsf.context.symbol.source.ISymbolSourceProvider;
import org.eclipse.jst.jsf.designtime.symbols.FileContextUtil;
import org.eclipse.jst.jsf.designtime.symbols.SymbolUtil;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

public class SpringBeanProviderFactory extends
		AbstractSymbolSourceProviderFactory implements ISymbolSourceProvider {

	/**
	 * @see org.eclipse.jst.jsf.context.symbol.source.ISymbolSourceProvider#getSymbols(org.eclipse.core.runtime.IAdaptable,int)
	 */
	@Override
	public ISymbol[] getSymbols(IAdaptable context, int symbolScopeMask) {
		final List<ISymbol> symbols = new ArrayList<ISymbol>();
		IJavaProject javaProject = JavaCore.create(this.getProject());
		createSpringBeanSuggestions(symbols, javaProject);
		createRegExSuggestions(symbols, javaProject);

		return symbols.toArray(ISymbol.EMPTY_SYMBOL_ARRAY);
	}

	private void createRegExSuggestions(List<ISymbol> symbols,
			IJavaProject javaProject) {
		try {
			IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
			
			String basepackage = preferenceStore.getString(JsfcaPerferenceConstants.PROP_KEY_BASE_PACKAGE);
			String regEx = preferenceStore.getString(JsfcaPerferenceConstants.PROP_KEY_FILE_PATTERN);
			
			if(basepackage == null || basepackage.isEmpty()){
				return;
			}
			
			if(regEx == null || regEx.isEmpty()){
				return;
			}
			
			Pattern classPattern = Pattern.compile(regEx);
			
			IPackageFragment[] packageFragments = javaProject
					.getPackageFragments();
			for (IPackageFragment packageFragment : packageFragments) {
				if (packageFragment.getElementName().startsWith(
						basepackage.toLowerCase())) {
					if(packageFragment.containsJavaResources()){
						ICompilationUnit[] compilationUnits = packageFragment.getCompilationUnits();
						for(ICompilationUnit compUnit : compilationUnits){
							IType primaryType = compUnit.findPrimaryType();
							if(primaryType!=null && classPattern.matcher(primaryType.getElementName()).matches()){
								final IBeanInstanceSymbol bean = SymbolFactory.eINSTANCE
										.createIBeanInstanceSymbol();
								bean.setName(getBeanName(primaryType.getElementName()));
								IJavaTypeDescriptor2 beanJavaTypeDescriptor = SymbolFactory.eINSTANCE
										.createIJavaTypeDescriptor2();
								beanJavaTypeDescriptor.setType(primaryType);
								bean.setJavaTypeDescriptor(beanJavaTypeDescriptor);
								symbols.add(bean);
							}
						}
					}
				}
			}

		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}
	
	private String getBeanName(String simpleClassName){
		StringBuilder sb = new StringBuilder();
		sb.append(simpleClassName.substring(0,1).toLowerCase());
		sb.append(simpleClassName.substring(1));
		return sb.toString();
	}

	private void createSpringBeanSuggestions(final List<ISymbol> symbols,
			IJavaProject javaProject) {
		Set<IBeansConfig> configs = new HashSet<IBeansConfig>();
		IBeansProject springProject = BeansCorePlugin.getModel().getProject(
				this.getProject());

		configs.addAll(springProject.getConfigs());
		Set<IBeansConfigSet> configSets = springProject.getConfigSets();
		for (IBeansConfigSet configSet : configSets) {
			Set<IBeansConfig> bcs = configSet.getConfigs();
			configs.addAll(bcs);
		}

		for (IBeansConfig bc : configs) {
			Set<String> beanClasses = new HashSet<String>(bc.getBeanClasses());

			Set<IBeansComponent> components = bc.getComponents();
			Set<IBean> springBeans = new HashSet<IBean>();
			for (IBeansComponent comp : components) {
				springBeans.addAll(comp.getBeans());
			}

			for (IBean springBean : springBeans) {
				beanClasses.remove(springBean.getClassName());
				final IBeanInstanceSymbol bean = SymbolFactory.eINSTANCE
						.createIBeanInstanceSymbol();
				bean.setName(springBean.getElementName());
				IType beanClass = null;
				try {
					beanClass = javaProject.findType(springBean.getClassName());
				} catch (JavaModelException e) {
					e.printStackTrace();
				}
				IJavaTypeDescriptor2 beanJavaTypeDescriptor = SymbolFactory.eINSTANCE
						.createIJavaTypeDescriptor2();
				beanJavaTypeDescriptor.setType(beanClass);
				bean.setJavaTypeDescriptor(beanJavaTypeDescriptor);
				symbols.add(bean);
			}

			for (String beanClass : beanClasses) {
				final IBeanInstanceSymbol bean = SymbolFactory.eINSTANCE
						.createIBeanInstanceSymbol();
				bean.setName(getDefaultComponentName(beanClass));
				IType beanType = null;
				try {
					beanType = javaProject.findType(beanClass);
				} catch (JavaModelException e) {
					e.printStackTrace();
				}
				IJavaTypeDescriptor2 beanJavaTypeDescriptor = SymbolFactory.eINSTANCE
						.createIJavaTypeDescriptor2();
				beanJavaTypeDescriptor.setType(beanType);
				bean.setJavaTypeDescriptor(beanJavaTypeDescriptor);
				symbols.add(bean);
			}
		}
	}

	private String getDefaultComponentName(String beanClass) {
		int lastIndexOfDot = beanClass.lastIndexOf('.') + 1;
		StringBuilder sb = new StringBuilder();
		sb.append(beanClass.substring(lastIndexOfDot, lastIndexOfDot + 1)
				.toLowerCase());
		sb.append(beanClass.substring(lastIndexOfDot + 1));

		return sb.toString();
	}

	/**
	 * @see org.eclipse.jst.jsf.context.symbol.source.ISymbolSourceProvider#getSymbols(java.lang.String,
	 *      org.eclipse.core.runtime.IAdaptable, int)
	 */
	@Override
	public ISymbol[] getSymbols(String prefix, IAdaptable context,
			int symbolScopeMask) {
		return SymbolUtil.filterSymbolsByPrefix(
				getSymbols(context, symbolScopeMask), prefix);
	}

	/**
	 * @see org.eclipse.jst.jsf.context.symbol.source.ISymbolSourceProvider#isProvider(org.eclipse.core.runtime.IAdaptable)
	 */
	@Override
	public boolean isProvider(IAdaptable context) {
		IFile file = FileContextUtil.deriveIFileFromContext(context);
		return (file != null && file.getProject() == this.getProject());
	}

	/**
	 * @see org.eclipse.jst.jsf.context.symbol.source.AbstractSymbolSourceProviderFactory#create(org.eclipse.core.resources.IProject)
	 */
	@Override
	protected ISymbolSourceProvider create(IProject project) {
		return this;
	}
}
